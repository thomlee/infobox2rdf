#!/usr/bin/perl

use strict;
use DBI;

my $psql_db = shift;
my $psql_user = shift;
my $psql_pwd = shift;
my $sqlite_db = shift;

die "Usage: $0 <source DB name> <source DB username> <source DB password> <target DB filepath>\n"
    unless defined($psql_db) &&
    defined($psql_user) &&
    defined($psql_pwd) &&
    defined($sqlite_db);

my $db2 = DBI->connect("DBI:Pg:dbname=$psql_db", $psql_user, $psql_pwd)
    || die $DBI::errstr;

my $db = DBI->connect("dbi:SQLite:".$sqlite_db, undef, undef)
    || die $DBI::errstr;

print "Deleting existing data...\n";
$db->do("DROP TABLE IF EXISTS triplets")
    || die $db->errstr;
$db->do(q(CREATE TABLE triplets (
    subject VARCHAR NOT NULL,
    predicate VARCHAR NOT NULL,
    object VARCHAR NOT NULL)))
    || die $db->errstr;

$db->{AutoCommit} = 0;

my $statement_insert_triplet = $db->prepare(q(INSERT INTO triplets (
    subject, predicate, object) VALUES(?, ?, ?)))
    || die $db->errstr;

my $triplet = $db2->prepare("SELECT subject, predicate, object FROM triplets")
    || die $db2->errstr;
$triplet->execute();

print "Transferring database records...\n";
my $insert_count = 0;
my $batch_size = 500000;
my $count = 0;
while(my @rows = $triplet->fetchrow_array){
    $statement_insert_triplet->execute(@rows) || die $statement_insert_triplet->errstr;

    ++$insert_count;
    if ($insert_count == $batch_size) {
	print "Committing...\r";
	$db->commit || $db->errstr;
	$insert_count = 0;
    }

    $count++;
    print "$count records transferred\r";
}
$statement_insert_triplet->finish;
$triplet->finish;

if ($count > 0) {
    print "\n";
}

if ($insert_count > 0) {
    print "Committing...\r";
    $db->commit || $db->errstr;
}

print "Creating indices...\r";
$db->do("CREATE INDEX index_subject on triplets (subject)")
    || die $db->errstr;
$db->do("CREATE INDEX index_predicate on triplets (predicate)")
    || die $db->errstr;
print "Committing...                  \r";
$db->commit || die $db->errstr;

if ($count > 0) {
    print "\n";
}
print "Transfer finished\n";

$db->{AutoCommit} = 1;

undef $triplet;
undef $statement_insert_triplet;
$db->disconnect || die $db->errstr;
$db2->disconnect || die $db2->errstr;
