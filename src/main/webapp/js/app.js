(function($){
  $.ajaxSetup({
    'beforeSend': function(xhr) {
        xhr.setRequestHeader('Accept', 'text/javascript');
    }
  });

  $("body").live('resize', function() {
      console.log('resize');
      $('body').css('height', '100%');
  });

  var Templates = {
    user : '<div>\
      <div>signed in as <strong>{{name}}</strong> <a href="/disconnect">Sign out</a></div>\
    </div>',
    groups: '<div id="group-list">\
      <h2>Find your community</h2>\
      <h3>Select one of your Meetup groups</h3>\
      <div id="groups">\
        <ul>\
        {{#groups}}\
         <li data-name="{{name}}"><a href="#!/{{slug}}" class="group">{{name}}</a></li>\
        {{/groups}}\
       </ul>\
      <div>\
    </div>',
    groupPage: '<h1>{{name}}</h1>\
      visit this group on <a href="{{link}}" target="_blank">meetup.com</a>\
      <h2>Community Polls</h2>\
      <div id="create"><a href="#new_poll" class="btn" id="create-poll">Create a new Poll</a></div>\
      <div id="feedback"></div>\
     <ul id="polls"></ul>',
    noPolls: '<li><div><strong>:/</strong> This community is doesn\'t have any polls just yet. Be the <strong>first</strong> to spark on.</div></li>',
    polls: '{{#polls}}<li><a href="/polls/{{id}}">{{name}}</div></li>{{/polls}}',
    newPoll: '<li><a href="/polls/{{id}}">{{name}}</div></li>',
    pollForm: '<div><form id="new-poll" action="#" method="POST">\
      <h3>Get something out of your community</h3>\
      <input type="hidden" name="group-urlname" value="{{slug}}">\
      <div id="errors"></div>\
      <ul id="fields">\
        <li>\
          <label for="name">What do you want to call this poll?</label>\
          <span><input name="name" type="text" /></span>\
        </li>\
        <li>\
          <label for="starts">When will this start?</label>\
          <span><input type="text" name="ends" class="time" value="now" /></span></li>\
        <li>\
          <label for="ends">When will this end?</label>\
          <span><input type="text" name="ends" class="time" value="later" /></span>\
        </li>\
        <li>\
         <label for="description">What are you asking?</label>\
         <span><textarea name="description" /></span>\
        </li>\
      </ul>\
      <a href="#add-option" id="add-option">Add a choice</a>\
      <div class="hint">you can always edit these later</div>\
      <div><input type="submit" class="btn" value="Create this poll"/> or <a href="" id="cancel-new-poll">Maybe later</a></div>\
   </form></div>',
    newOption: '<li><textarea class="option" name="choice"/></li>',
    newPollErrors: '<ul>{{#errors}}<li>{{.}}</li>{{/errors}}</ul>'
  }, render = function(view, data) {
      return Mustache.to_html(view, data);
  }, urlFragment = function() {
      var parts = window.location.href.split("#!/");
      if(parts.length > 1) { return parts[1].split("/"); }
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
            console.log("showing group " + group);
            $("#content").html(render(Templates.groupPage, group));

            $("#create-poll").live('click', function(e) {
              e.preventDefault();
              $("#create").html(render(Templates.pollForm, group));
              $("#new-poll").submit(function(e) {
                e.preventDefault();
                $.post("/polls.json", $(this).serialize(), function(data) {
                  if(data.errors) {
                    $("#errors").fadeOut().html(render(Templates.newPollErrors, data)).fadeIn();
                  } else {
                    $("#errors").empty();
                    console.log(data);
                      if(data.poll) {
                        $("#create").fadeOut(function(){

                          $("#polls").append(render(Templates.newPoll, data.poll));
                          $("#feedback").hide().html("Created new Poll " + data.poll.name).fadeIn(function(){
                              var self = $(this), done = function() {
                                self.fadeOut(function() {
                                     $("#create").html('<a href="#new_poll" class="btn" id="create-poll">Create a new Poll</a>').fadeIn();
                                });
                              };
                              setTimeout(done, 2500);
                          });
                        });
                      }
                  }
                }, "json");
                return false;
              });
              $("#cancel-new-poll").click(function(e) {
                e.preventDefault();
                showGroup(group);
                return false;
              });
              $("#add-option").click(function(e) {
                 e.preventDefault();
                  $("#fields").append(Templates.newOption);
                 return false;
              });

            });
            $.get("/polls.json", { group : group.slug }, function(data){
                if(data.polls) {
                  $("#polls").html(render(Templates.polls, data));
                } else {
                  $("#polls").html(render(Templates.noPolls));
                }
            });

        };

        var frag = urlFragment()
        if(frag) {
          var group = findGroup(frag[0]);
          if(group) {
            showGroup(group);
          } else {
            listGroups();
          }
        } else {
          listGroups();
        }

        $(".group").live('click', function(e) {
          var link = $(this), slug = link.attr("href").replace("#!/",""), group = findGroup(slug);
          if(group) {
            showGroup(group);
          }
        });
      }
    }
  }, "json");
})(jQuery);