#!/usr/bin/env ruby

require 'find'

UNPACK_DIR='/mnt/StackLinks/unpack'

Find.find '/mnt/StackLinks/downloads' do |f| 
	if f.end_with?('7z')  or f.end_with?('7z.001')
		sitename = File.basename(f)
		sitename = sitename[0..sitename.rindex('.7z')-1]
		unless sitename.start_with?('meta.')
			puts "processing #{sitename} '#{f}'"
			list = %x{/usr/bin/7z l '#{f}'}
			
			postsline = list.lines.find { |l| l.strip().end_with? 'posts.xml' }
			postfile = postsline[53..-1].strip()
			timebit = postfile[0,6]
			
			puts "Unpacking '#{f}' - '#{postfile}'"
			%x{/usr/bin/7z e -y -o#{UNPACK_DIR} '#{f}' '#{postfile}'}
			postout="#{UNPACK_DIR}/#{sitename}-#{timebit}.posts.xml"
			File.rename("#{UNPACK_DIR}/posts.xml",postout) 
			puts "recompressing #{postout}"
			%x{/bin/gzip -f '#{postout}'}
		end
	end
end

puts "*** finished unpacking"
Find.find UNPACK_DIR do |f|
	if f.end_with? '.xml.gz' 
		puts "uploading '#{f}'"
		%x{/usr/bin/s3cmd --no-progress put '#{f}' s3://stacklinks-dl.logicalpractice.com/datadump/}
	end
end
