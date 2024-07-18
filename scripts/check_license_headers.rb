#!/usr/bin/env ruby
require 'find'

files = []

Find.find('.') do |path|
    next unless File.file?(path) && path.end_with?('.kt')

    lines = File.readlines(path)

    files << path unless lines[0].start_with?("/*") && lines[1].start_with?(" * MIT License")
end

if files.empty?
    puts "No missing license files found"
else
    puts "#{files.size} files with missing license files found"
    files.each {|file| puts file}
    exit(1)
end
