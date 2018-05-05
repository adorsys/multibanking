#!/usr/bin/perl -w

sub login {
	my($user)=shift;
	my($password)=shift;
	my($url)=shift;

	my $ua = LWP::UserAgent->new;
	my $server_endpoint = $url;
    my $JSON = JSON->new->utf8;
    $JSON->convert_blessed(1);

	my $req = HTTP::Request->new(POST => $server_endpoint);
	$req->header('content-type' => 'application/json');
	my($json)=$JSON->encode(new Login($user, $password));
	$req->content($json);

	my $resp = $ua->request($req);
	if (! $resp->is_success) {
		print "ERROR for login for user $user : ", $resp->code( ), "\n";
	}
	my($token) = $resp->content();
	# my($token) = decode_json($resp->content())->{id_token};
	return $token;
}

sub bankAccess {
	my($url)=shift;
	my($token)=shift;
	my($content)=shift;

	my $ua = LWP::UserAgent->new;
	my $server_endpoint = $url;

	my $req = HTTP::Request->new(POST => $server_endpoint);
	$req->header('content-type' => 'application/json');
	$req->header('Authorization' => "Bearer $token");
	$req->content($content);

	my $resp = $ua->request($req);
	if (! $resp->is_success) {
		print "ERROR for get bank Access : ", $resp->code( ), "\n";
	}
	my($ba) = $resp->content();
	# my($token) = decode_json($resp->content())->{id_token};
	return $ba;
}

package Login;
sub new {

   my $class = shift;

   my $self = {
      username => shift,
      password  => shift,
   };

   bless $self, $class;
   return $self;
}
sub TO_JSON { return { %{ shift() } }; }

package main;

use strict;
use LWP::UserAgent;
use JSON;

my($num_args) = $#ARGV + 1;

my($url)="http://localhost:8080";
my($bac)="{\"bankName\":\"Mock\",\"bankLogin\":\"s.schaefer7\",\"bankLogin2\":null,\"bankCode\":\"19999999\",\"tanTransportTypes\":{},\"hbciPassportState\":null,\"externalIdMap\":{},\"id\":null,\"userId\":null,\"pin\":\"12345\",\"storePin\":false,\"temporary\":false,\"storeBookings\":false,\"categorizeBookings\":true,\"storeAnalytics\":false,\"storeAnonymizedBookings\":false}";
print $bac;
my($token) = login ("admin", "geheim", "$url/api/v1/auth/mobile");
print "token ist: $token\n";
my($ba) = bankAccess("$url/api/v1/bankaccesses", "$token", "$bac");
