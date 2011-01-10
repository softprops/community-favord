(function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
        xhr.setRequestHeader('Accept', 'text/javascript');
    }
  });

  var Templates = {
    user : '<div>\
      <div>{{name}} <a href="/disconnect">Sign out</a></div>\
    </div>',
    groups: '<div id="group-list">\
      <h3>Select a one of your groups</h3>\
      <div id="groups">\
        <ul>\
        {{#groups}}\
         <li><a href="#{{slug}}" class="group">{{name}}</a></li>\
        {{/groups}}\
       </ul>\
      <div>\
    </div>',
    groupPage: '<h2>{{name}}</h2>\
      visit this group on <a href="{{link}}">meetup.com</a>\
      <h3>Polls</h3>\
      <div id="create"><a href="/polls/new" class="btn" id="create-poll">Create a new Poll<a></div>\
    <div id="polls"/>',
    pollForm: '<div><form id="new-poll">\
      <input type="hidden" name="group-urlname" value="{{urlname}}">\
      <ul>\
        <li><label for="name">What do you want to call this poll?</label><span><input name="name" type="text" /></span></li>\
        <li><label for="starts">When will this start?</label><span><input type="text" name="ends" class="time" value="now" /></span></li>\
        <li><label for="ends">When will this end?</label><span><input type="text" name="ends" class="time" value="later" /></span></li>\
        <li><label for="explain">What are you asking?</label><span><textarea name="explain" /></span></li>\
      </ul>\
      <a href="#add-option" id="add-option">Add an option</a>\
   </form></div>'
  }, render = function(view, data) {
      return Mustache.to_html(view, data);
  }, urlFragment = function() {
      var parts = window.location.href.split("#");
      if(parts.length > 1) { return parts[1]; }
  };

  $.get("/user.json", function(data) {
    if(data && data.user && data.user.member) {
      $("#connect").remove();
      $("#user").html(render(Templates.user, data.user.member));
      if(data && data.user.groups) {
        var groups = data.user.groups, findGroup = function(slug) {
          for(g in groups) {
            var group = groups[g];
            if(group.slug === slug) { return group; }
          }
        }, listGroups = function() {
            $("#content").html(render(Templates.groups, data.user));
        }, showGroup = function(group) {
            $("#content").html(render(Templates.groupPage, group));
            //$.get("/polls.js", { group : group.urlname }, function(data) {
            //  console.log(data);
            //});
            $("#create-poll").click(function(e) {
              e.preventDefault();
              $("#create").html(render(Templates.pollForm, group));
              return false;
            });
        };

        var slug = urlFragment()
        if(slug) {
          var group = findGroup(slug);
          if(group) {
            showGroup(group);
          } else {
            listGroups();
          }
        } else {
          listGroups();
        }

        $(".group").live('click', function(e) {
          var link = $(this), slug = link.attr("href").replace("#",""), group = findGroup(slug);
          if(group) {
            showGroup(group);
          }
        });
      }
    }
  }, "json");
})(jQuery);