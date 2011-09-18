#!/usr/bin/env ruby

REMOTE_HOST="ec2-107-20-129-20.compute-1.amazonaws.com"
BUNDLE_PREFIX="dl"
SSH_KEY="~/.ssh/ec2d.pem"
EC2_CERT=ENV['EC2_CERT']
EC2_PRIVATE_KEY=ENV['EC2_PRIVATE_KEY']

puts "Uploading x.509 certs"
%x{scp -q -i #{SSH_KEY} #{EC2_CERT} #{EC2_PRIVATE_KEY} ubuntu@#{REMOTE_HOST}:}

cert_file = File.basename EC2_CERT
private_key_file = File.basename EC2_PRIVATE_KEY

puts "Removing any previous images"
%x{ssh -q -i #{SSH_KEY} ubuntu@#{REMOTE_HOST} 'sudo rm /mnt/#{BUNDLE_PREFIX}*'}
puts "Running ec2-bundle-vol"
%x{ssh -q -i #{SSH_KEY} ubuntu@#{REMOTE_HOST} 'sudo ec2-bundle-vol --cert ~/#{cert_file} --privatekey ~/#{private_key_file} --arch i386 --size 1536 --user 936465047679 --batch --prefix #{BUNDLE_PREFIX} --inherit --destination /mnt'}

puts "Running ec2-upload-bundle"
%{ssh -q -i #{SSH_KEY} ubuntu@#{REMOTE_HOST}  'sudo ec2-upload-bundle --bucket stacklinks-dl.logicalpractice.com --access-key #{ENV['AMAZON_ACCESS_KEY_ID']} --secret-key #{ENV['AMAZON_SECRET_ACCESS_KEY']} --manifest /mnt/#{BUNDLE_PREFIX}.manifest.xml'}


