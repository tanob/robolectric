#!/usr/bin/env ruby

require 'json'

class GitHub
    def initialize(user, pass)
        @user = user
        @pass = pass
    end

    def get(path)
        puts "GET #{path}"
        response = `curl -s -u '#{@user}:#{@pass}' https://api.github.com#{path}`
        puts "Response:\n#{response}"
        response
    end

    def post(path, params)
        puts "POST #{path}"
        params_json = params.to_json
        cmd = "curl -d '#{params_json}' -s -u '#{@user}:#{@pass}' https://api.github.com#{path}"
        response = `#{cmd}`
        puts "Response:\n#{response}"
        response
    end

    def delete(path)
        puts "DELETE #{path}"
        response = `curl -X DELETE -s -u '#{@user}:#{@pass}' https://api.github.com#{path}`
        puts "Response:\n#{response}"
        response
    end

    def upload_to_s3(create_download_json, file_path)
        puts "UPLOADING TO S3..."
        s3_url = create_download_json['s3_url']

        form_fields = {
            "key" => create_download_json['path'],
            "acl" => create_download_json['acl'],
            "success_action_status" => 201,
            "Filename" => create_download_json['name'],
            "AWSAccessKeyId" => create_download_json['accesskeyid'],
            "Policy" => create_download_json['policy'],
            "Signature" => create_download_json['signature'],
            "Content-Type" => create_download_json['mime_type'],
            "file" => "@" + file_path,
        }.map {|k,v| "-F '#{k}=#{v}'" }.join(' ')

        response = `curl -i #{form_fields} #{s3_url}`
        puts "Response:\n#{response}"
        response
    end
end

new_download_filepath = ARGV[0]
new_download_filename = File.basename new_download_filepath
new_download_description = ARGV[1] || "No description"

github_user = `git config --get github.user`.chomp

puts "Using GitHub user '#{github_user}'"
github_pass = ENV['github_pass']

if github_pass.nil? then
    puts "ERROR: Please set your password in the environment variable 'github_pass'"
    exit
end

github = GitHub.new(github_user, github_pass)

downloads_json = github.get("/repos/#{github_user}/robolectric/downloads")
downloads = JSON.parse(downloads_json)

existent_download = downloads.find {|download| download['name'] == new_download_filename }
delete_existent_download_json = github.delete("/repos/#{github_user}/robolectric/downloads/#{existent_download['id']}")

new_download_json = github.post("/repos/#{github_user}/robolectric/downloads", { "name" => new_download_filename, "size" => File.new(new_download_filepath).size, "description" => new_download_description })
github.upload_to_s3(JSON.parse(new_download_json), new_download_filepath)

