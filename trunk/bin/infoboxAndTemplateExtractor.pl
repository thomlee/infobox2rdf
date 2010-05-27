#!/usr/bin/perl

use strict;
use warnings;
use Parse::MediaWikiDump;
use XML::Writer;

my $infile = \*STDIN;
my ($outfile_infobox, $outfile_template) = (shift, shift);

open(Fout1, ">$outfile_infobox") or die "cannot open file: $!\n";
open(Fout2, ">$outfile_template") or die "cannot open file: $!\n";

my $output_infobox = \*Fout1;
my $output_template = \*Fout2;

my $template_name_len = length 'Infobox';
my $pages = Parse::MediaWikiDump::Pages->new($infile);
my $writer_infobox = new XML::Writer(OUTPUT => $output_infobox,
			     DATA_MODE => 1,
			     DATA_INDENT => 2,
			     ENCODING => 'utf-8');

my $writer_template = new XML::Writer(OUTPUT => $output_template,
                 DATA_MODE => 1,
                 DATA_INDENT => 2,
                 ENCODING => 'utf-8');

binmode(STDIN, ':utf8');
binmode(Fout1, ':utf8');
binmode(Fout2, ':utf8');

if ($pages->case ne 'first-letter') {
  die "unable to handle any case setting besides 'first-letter'";
}

$writer_infobox->startTag('mediawiki');
$writer_infobox->startTag('siteinfo');
$writer_infobox->startTag('sitename');
$writer_infobox->characters($pages->sitename);
$writer_infobox->endTag('sitename');
$writer_infobox->startTag('base');
$writer_infobox->characters($pages->base);
$writer_infobox->endTag('base');
$writer_infobox->endTag('siteinfo');

$writer_template->startTag('mediawiki');
$writer_template->startTag('siteinfo');
$writer_template->startTag('sitename');
$writer_template->characters($pages->sitename);
$writer_template->endTag('sitename');
$writer_template->startTag('base');
$writer_template->characters($pages->base);
$writer_template->endTag('base');
$writer_template->endTag('siteinfo');

my $page_count = 0;

while(defined(my $page = $pages->next)) {
    my $t = $page->title;
    if($t =~ m/Template:.*/){

       $writer_template->startTag('template');
       $writer_template->startTag('name');
       my $p = $page->title;
       my $len = rindex $p."\$","\$";
       $p = substr($p,9,$len);
       $writer_template->characters($p);
       $writer_template->endTag('name');
         if ($page->text) {
            $writer_template->startTag('content');
            my $text = $page->text;
            my $content = $$text;
            $content =~ s/<noinclude>.*?<\/noinclude>//sg;
            $content =~ s/<\/noinclude>//sg;
            $content =~ s/<noinclude>//sg;
            $content =~ s/<\/noinclude//sg;
            $content =~ s/\/noinclude>//sg;
            $content =~ s/<noinclude//sg;
            $content =~ s/noinclude>//sg;
            $writer_template->characters($content);
            $writer_template->endTag('content');
         }
    
       $writer_template->endTag('template');
    }

    $writer_infobox->startTag('page');
    $writer_infobox->startTag('title');
    $writer_infobox->characters($page->title);
    $writer_infobox->endTag('title');
    if ($page->namespace) {
        $writer_infobox->startTag('namespace');
        $writer_infobox->characters($page->namespace);
        $writer_infobox->endTag('namespace');
    }
    $writer_infobox->startTag('id');
    $writer_infobox->characters($page->id);
    $writer_infobox->endTag('id');
    if ($page->text) {
	write_templates($page->text);
    }
    my $cats = $page->categories;
    foreach my $cat (@$cats) {
	$writer_infobox->startTag('category');
	$writer_infobox->characters($cat);
	$writer_infobox->endTag('category');
    }
    
    $writer_infobox->endTag('page');

    $page_count++;
    print "Processed $page_count pages\r";
}
$writer_infobox->endTag('mediawiki');
$writer_infobox->end();
#$outfile_infobox->close();
close Fout1;

$writer_template->endTag('mediawiki');
$writer_template->end();
#$outfile_template->close();
close Fout2;

if ($page_count > 0) {
    print "\n";
}
print "Process completed\n";

exit 0;

sub write_templates {
    my $text = shift;
    my $text_len = length $$text;
    my $pos = 0;
    my $pos2 = 0;
    my $pos3 = 0;
    while (($pos = index $$text, 'Infobox', $pos) > -1) {
      my $is_infobox = 0;
      my $c = '';
      for ($pos2 = $pos - 1; $pos2 > 0; $pos2--) {
	$c = substr $$text, $pos2, 1;
	if ($c eq ' ') {
	    next;
	}
	elsif ($c eq '{') {
	    $pos2--;
	    if ($pos2 > -1) {
		$c = substr $$text, $pos2, 1;
		if ($c eq '{') {
		    $is_infobox = 1;
		}
	    }
	    last;
	}
	else {
	    last;
	}
      }
      $pos += $template_name_len;
      if (!$is_infobox) {
	  next;
      }
      my $braces = 1;
      my $brackets = 0;
      $pos3 = $pos;
      $pos2 = $pos3;
      my $is_infobox_name = 1;
      my $is_infobox_property = 0;
      my $is_infobox_value = 0;
      my $infobox_name = '';
      my $infobox_property = '';
      my $infobox_value = '';
      my %infobox_map = ();
      for (; $pos3 < $text_len; $pos3++) {
	  $c = substr $$text, $pos3, 1;
	  if ($c eq '|' && $braces == 1 && $brackets ==0) {
	      if ($is_infobox_name) {
		  $infobox_name = normalize (substr $$text, $pos2, $pos3 - $pos2);
		  $infobox_name =~ s/^_+//g;
		  $is_infobox_name = 0;
		  $is_infobox_property = 1;
		  $pos2 = $pos3 + 1;
	      }
	      if ($is_infobox_value) {
		  if (!($infobox_property eq '')) {
		      $infobox_value = normalize (substr $$text, $pos2, $pos3 - $pos2);
		      $infobox_map{$infobox_property} = $infobox_value;
		      $infobox_property = '';
		      $infobox_value = '';
		  }
		  $is_infobox_value = 0;
		  $is_infobox_property = 1;
		  $pos2 = $pos3 + 1;
	      }
	  }
	  elsif ($c eq '=' && $braces == 1 && $brackets == 0) {
	      if ($is_infobox_property) {
		  $infobox_property = normalize (substr $$text, $pos2, $pos3 - $pos2);
		  $is_infobox_name = 0;
		  $is_infobox_property = 0;
		  $is_infobox_value = 1;
		  $pos2 = $pos3 + 1;
	      }
	  }
	  elsif ($c eq '{') {
	      $pos3++;
	      if ($pos3 < $text_len) {
		  $c = substr $$text, $pos3, 1;
		  if ($c eq '{') {
		      $braces++;
		  }
	      }
	      else {
		  last;
	      }
	  }
	  elsif ($c eq '}') {
	      $pos3++;
	      if ($pos3 < $text_len) {
		  $c = substr $$text, $pos3, 1;
		  if ($c eq '}') {
		      $braces--;
		  }
		  if ($braces == 0) {
#		      my $len = $pos3 - $pos - 1;
#		      $writer_infobox->startTag('infobox');
#		      $writer_infobox->characters(substr $$text, $pos, $len);
#		      $writer_infobox->endTag('infobox');
		      if ($is_infobox_value) {
			  if (!($infobox_property eq '')) {
			      $infobox_value = normalize (substr $$text, $pos2, $pos3 - $pos2 - 1);
			      $infobox_map{$infobox_property} = $infobox_value;
			  }
		      }
		      $writer_infobox->startTag('infobox');
		      $writer_infobox->startTag('name');
		      $writer_infobox->characters($infobox_name);
		      $writer_infobox->endTag('name');
		      while (($infobox_property,$infobox_value) = each(%infobox_map)) {
			  $writer_infobox->startTag('entry');
			  $writer_infobox->startTag('property');
			  $writer_infobox->characters($infobox_property);
			  $writer_infobox->endTag('property');
			  $writer_infobox->startTag('value');
			  $writer_infobox->characters($infobox_value);
			  $writer_infobox->endTag('value');
			  $writer_infobox->endTag('entry');
		      }
		      $writer_infobox->endTag('infobox');
		      last;
		  }
	      }
	      else {
		  last;
	      }
	  }
	  elsif ($c eq '[') {
	      $pos3++;
	      if ($pos3 < $text_len) {
		  $c = substr $$text, $pos3, 1;
		  if ($c eq '[') {
		      $brackets++;
		  }
	      }
	      else {
		  last;
	      }
	  }
	  elsif ($c eq ']') {
	      $pos3++;
	      if ($pos3 < $text_len) {
		  $c = substr $$text, $pos3, 1;
		  if ($c eq ']') {
		      $brackets--;
		  }
	      }
	      else {
		  last;
	      }
	  }
      }
      $pos = $pos3 + 1;
    }
}

sub normalize {
    my $str = shift;
    $str =~ s/^\s+//g;
    $str =~ s/\s+$//g;
    $str =~ s/\s+/ /g;
    return $str;
}
