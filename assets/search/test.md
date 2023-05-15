---
---
将search.json文件添加到keep_files
module Jekyll

  require 'pathname'
  require 'json'

  class SearchFileGenerator < Generator
     safe true

     def generate(site)
       output = [{"title" => "Test"}]

       path = Pathname.new(site.dest) + "search.json"

       FileUtils.mkdir_p(File.dirname(path))
       File.open(path, 'w') do |f|
         f.write("---\nlayout: null\n---\n")
         f.write(output.to_json)
       end
       site.keep_files << "search.json"
     end
   end
end

将新页面添加到site.pages：
module Jekyll
  class SearchFileGenerator < Generator
    def generate(site)
      @site  = site
      search = PageWithoutAFile.new(@site, site.source, "/", "search.json")
      search.data["layout"] = nil
      search.content = [{"title" => "Test 32"}].to_json
      @site.pages << search
    end
  end
end