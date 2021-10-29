  jQuery(document).ready(function ($) {

  // set csrf token for login
    (function(){
        $.ajax({
            url: 'https://auth.geeksforgeeks.org/setLoginToken.php',
            type: 'POST',
	    xhrFields: {
                 withCredentials: true
            },
            success: function(data){
            },
            error: function(data){
                console.log(data);
            }
        });
    })();

  $('#loginCaptcha').closest('.modal-form-group').hide();
  var redirectUrl = $('#Login').find('input[name=to]').val();

// pop up modal when button clicks.
  $('body').on('click', '.login-modal-btn', function(e){
    e.preventDefault();
    var href = window.location.href;
    if($(this).prop("tagName") == "A"){
        href = $(this).attr('href');
    }
    else if($(this).prop("tagName") == "FORM"){
        href = $(this).attr('action');
    }
    $('.login-modal-div').find('input[name=to]').val(href);
    $('.login-modal-div').fadeIn('fast');
    redirectUrl = href;
  });

  // check required field.
  $('body').on('blur', 'input[required=required]', function(){
    var val = $(this).val();
    $('.error-focus').removeClass('error-focus');
    $('div.input-error').remove();
    if(val == "" || val == null || val == undefined){
      $(this).closest('.modal-form-group').append('<div class="input-error">Field can not be empty.</div>');
      $(this).addClass('error-focus');
      $(this).focus();
    }
  });

  //remove error message if input have some words.
  $('body').on('keydown', 'input[required=required]', function(){
     var val = $(this).val();
     if(val != "" || val != null || val != undefined){
       $(this).removeClass('error-focus');
       $(this).closest('.modal-form-group').find('.input-error').remove();
     }
  });

  
    // dismiss modal when click on close icon.
    $('body').on('click', '.close', function(){
      $(this).closest('.login-modal-div').fadeOut('fast');
    });

    //dismiss modal when esc key pressed.
    $(document).keypress(function(e) { 
        if (e.keyCode == 27) { 
            $(".login-modal-div").fadeOut('fast');
        } 
    });

    //dismiss modal when click outside of it.
    $('body').on('click', '.login-modal-div', function(){
        $(".login-modal-div").fadeOut('fast');
    });

    $('body').on('click', '.login-modal-div .modal-content', function(e){
        e.stopPropagation();
    });

    //toggle between forgot div and login div.
    $('body').on('click', '.login-link, .forgot-link', function(){
      if($(this).hasClass('login-link')){
        $('.forgot-div').slideUp();
        $('.login-register-div').slideDown();
      }
      else{
        $('.login-register-div').slideUp();
        $('.forgot-div').slideDown();
      }
    });

    // redirect function.
    function redirect(where) {
      if( where == 'to' ) {
        window.location.href = to;
      } else if( where == 'reset' ) {
        q2to3();
        $("#ruser").val($("#fuser").val());
      }
      else{
	if(window.location.href == where){
            window.location.reload(true);
        }
        else{
            window.location.href = where;
        }
      }
    }

    // event on submit either login, register or forgot form.
    $(".login-form").submit( function(e) {
      e.preventDefault();
      this1 = $(this);
      $('.spinner-loading-overlay').show();
      this1.find(".extra").empty();
      this1.find('input[type=submit]').attr('disabled', true);
      var browserInfo = fetchBrowserInfo();
      $.ajax({
        type: "POST",
        url: 'https://auth.geeksforgeeks.org/auth.php',
        data: $(this).serialize()+"&browserInfo="+JSON.stringify(browserInfo),
        xhrFields: {
          withCredentials: true
        },
        dataType: "json",
        success: function( data ) {
          this1.find('input[type=submit]').attr('disabled', false);
          if( data.redirect ) {
            redirect( data.redirect );
          } else if( data.extra ) {
            $('.spinner-loading-overlay').hide();
            this1.find(".extra").append(data.extra);
	    var errorTxt = this1.find(".extra").find('div').text().trim();
            grecaptcha.reset(loginWidgetId);
            if(errorTxt == "Captcha validation needed" && this1.attr('id') == "Login"){
              if(!$('#loginCaptcha').closest('.modal-form-group').find('input[name=recaptchaShow]').length){
                 $('#loginCaptcha').closest('.modal-form-group').append('<input name="recaptchaShow" type="hidden" value="1">');
              }
              $('#loginCaptcha').closest('.modal-form-group').show();
            }
          }
        },
        error: function(jqXHR, exception, errorThrown) {
          this1.find('input[type=submit]').attr('disabled', false);
          $('.spinner-loading-overlay').hide();
          console.log( "An error occurred" );
        },
        complete: function() {
          this1.find('input[type=submit]').attr('disabled', false);
          if(this1.attr('id') == "Forgot"){
            grecaptcha.reset(forgotWidgetId);
          }
          else{
            grecaptcha.reset(registerWidgetId);
          }
        }
      });
      return false;
    });

    //prevent to type space bar in register password field.
    $('body').on('keydown', 'input[name=pass]', function(e){
        if($(this).closest('form').find('input[name=reqType]').length && $(this).closest('form').find('input[name=reqType]').val() == "Register"){
            return e.which !== 32;
        }
    });


  // facebook login
      $('#fblogin').click(function(e){
        e.preventDefault();
        var w = 600, h = 350, left = screen.width / 2 - w / 2, top = screen.height / 2 - h / 2;
        var remember = $('#Login').find('input[name=rem]').is(":checked");
        var redirect = $('#Login').find('input[name=to]').val();
        var browserInfo = JSON.stringify(fetchBrowserInfo());
        window.open('https://auth.geeksforgeeks.org/fb-login.php?to='+redirect+'&rem='+remember+'&browserInfo='+browserInfo,'_self','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+',top='+top+', left='+left);
      }); 

  // linkedin login
  $('#inlogin').click(function(e){
      e.preventDefault();
      var w = 600, h = 350, left = screen.width / 2 - w / 2, top = screen.height / 2 - h / 2;
      var remember = $('#Login').find('input[name=rem]').is(":checked");
      var redirect = $('#Login').find('input[name=to]').val();
      var browserInfo = JSON.stringify(fetchBrowserInfo());
      window.open('https://auth.geeksforgeeks.org/linkedin-login.php?to='+redirect+'&rem='+remember+'&browserInfo='+browserInfo,'_self','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+',top='+top+', left='+left);
  });

  // github login
  $('#gitlogin').click(function(e){
      e.preventDefault();
      var browserInfo = JSON.stringify(fetchBrowserInfo());
      var w = 600, h = 350, left = screen.width / 2 - w / 2, top = screen.height / 2 - h / 2;
      var remember = $('#Login').find('input[name=rem]').is(":checked");
      var redirect = $('#Login').find('input[name=to]').val();
      window.open('https://auth.geeksforgeeks.org/github-login.php?to='+redirect+'&rem='+remember+'&browserInfo='+browserInfo,'_self','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+',top='+top+', left='+left);
  });

    
  $('#glogin').click(function(e){
    e.preventDefault();
    var remember = $('#Login').find('input[name=rem]').is(":checked");
    var redirect = $('#Login').find('input[name=to]').val();
    var browserInfo = JSON.stringify(fetchBrowserInfo());
    window.location = "https://auth.geeksforgeeks.org/googleLogin.php?redirect="+redirect+"&remember="+remember+"&browserInfo="+browserInfo;
  });

// never ever delete this code.
requirejs.onError = function (err) {
    if (err.requireType === 'mismatch') {
        // tell user
        console.log("error: "+err);
    } else {
        throw err;
    }
};

require.config({
    paths:{ 
		'typeahead': 'https://api.geeksforgeeks.org/js/typeahead.jquery.min',
		'bloodhound': 'https://api.geeksforgeeks.org/js/bloodhound.min'
	}
  });

require( ["typeahead", "bloodhound"],
    function (typeahead, Bloodhound) {
	// suggest organization.
      var instituteListBlood = new Bloodhound({
        initialize: false,
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        sufficient: 5,
        prefetch: { 
          url: 'https://api.geeksforgeeks.org/api/institutes/all'
        },
        remote: {
          url: 'https://api.geeksforgeeks.org/api/institutes/%QUERY/all/',
          wildcard: '%QUERY',
          filter: function (data) {
            instituteListBlood.add(data);
            return data;
          }
        }
      });

        instituteListBlood.clearPrefetchCache();
        instituteListBlood.initialize();
        $('input.typeahead').typeahead({
          minLength: 2,
          dynamic: false,
          highlight: true,
          cache: "sessionStorage",
          searchOnfocus: true,
          offset: true,
          blurOnTab: true
        }, {
          displaykey: 'value',
          limit: 15,
          source: instituteListBlood.ttAdapter(),
          accent: true,
          templates: {
          empty: [
                  ''
                  ].join('\n')
          }
        });
	});

    function fetchBrowserInfo(){
        var browserInfo = {};
        browserInfo.appName = navigator.appName;
        browserInfo.appCodeName = navigator.appCodeName;
        browserInfo.cookieEnable = navigator.cookieEnabled;
        browserInfo.prodName = navigator.product;
        browserInfo.appVersion = navigator.appVersion;
        browserInfo.appOs = navigator.platform;
        browserInfo.appLang = navigator.language;
        browserInfo.vendorName = navigator.vendor;
	browserInfo.loginDomain = "cdn";
        return browserInfo;
    }
  });



