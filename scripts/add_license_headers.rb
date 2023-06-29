#!/usr/bin/env ruby
require 'find'

license_text = "/*\n"
File.foreach('LICENSE') do |line|
  license_text = "#{license_text} * #{line}"
end
license_text = "#{license_text} */\n"

Find.find('.') do |path|
  next unless File.file?(path) && path.end_with?('.kt')

  content = File.read(path)

  # remove existing license
  if content.lines[0].start_with?('/*') && content.lines[1].start_with?(' * MIT License')
      content.sub!(/\/\*.*?\*\//m, '')
  end

  # remove existing newlines from start of file
  content.sub!(/\A\n*/, '')

  # Add new license
  content.prepend(license_text)

  File.write(path, content)
end
