---
---
<table>
    {% tablerow post in site.posts %}
      {{ post.title }}
    {% endtablerow %}
</table>
<hr>
<table>
    {% tablerow post in site.posts cols:3 %}
    {{ post.title }}
    {% endtablerow %}
</table>
<hr>
{%- if site.author and site.author.size <= 5 -%}
  Wow, {{ site.author }}, you have a long name!
{%- else -%}
  {%- assign username = site.author | slice: 6, 3 -%}
  {{ username }}
{%- endif -%}

{% raw %}
  In Handlebars, {{ this }} will be HTML-escaped, but
  {{{ that }}} will not.
{% endraw %}
<hr>
输出
In Handlebars, {{ this }} will be HTML-escaped, but {{{ that }}} will not.
<hr>
<hr>




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