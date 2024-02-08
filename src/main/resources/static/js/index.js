function activateExternalLinks() {
  $('a[href]')
    .filter(function() {
      var linkTarget = $(this).attr('href')
      var isExternalLink = false
      if (/^https?:\/\//.test(linkTarget)) {
        isExternalLink = true
      }
      return isExternalLink
    })
    .attr('target', '_blank')
}

function activatePopovers() {
  $('[data-toggle="popover"]').popover()
}

function appendQueryParameters() {
  var existingQueryParameters = window.location.search
  if (existingQueryParameters) {
    existingQueryParameters = new URLSearchParams(existingQueryParameters)
    existingQueryParameters.delete('language')
    var changeLocaleLink = document.querySelector('a#change-locale')
    var currentHref = changeLocaleLink.getAttribute('href')
    changeLocaleLink.setAttribute(
      'href',
      `${currentHref}&${existingQueryParameters.toString()}`,
    )
  }
}

/* v7 functions: */

function addDataLanguageChangeHandler() {
  $("#data-languages").change(function() {
    var url = window.location.href.split('?')[0];
    let dataLanguage = $("#data-languages").val();
    window.location.href = url + '?dataLanguage=' + dataLanguage;
  });
}

function addLanguageChangeHandler() {
  /* used in view pages to switch language tabs */
  $('.language-switcher').on('click', function() {
    // get the href attribute and cut off the leading hash to get the selected language
    var selectedLanguage = $(this).attr('href').slice(1);
    var editUrl = $('#edit-button').attr('href').split('?');
    var urlParams =
      editUrl.length > 1
        ? new URLSearchParams(editUrl[1])
        : new URLSearchParams('');
    urlParams.set('activeLanguage', selectedLanguage);
    editUrl = [editUrl[0], urlParams.toString()];
    $('#edit-button, #sticky-edit-button').attr('href', editUrl.join('?'));
  });
}

function addUserStatusChangeHandler(url) {
  /* used in users/view.html */
  const listener = function(enabled) {
    return async function(_evt) {
      try {
        const response = await fetch(url, {
          body: JSON.stringify({
            'enabled': enabled,
            'objectType': 'USER'
          }),
          headers: {
            'Content-Type': 'application/json'
          },
          method: 'PATCH'
        });
        if (!response.ok) {
          throw new Error('Error during change of user status');
        }
        window.location.reload();
      } catch (err) {
        console.error(err);
      }
    };
  };
  const btn = document.querySelector('input.user-status-toggle');
  const confirm = document.querySelector('a#confirm');
  const enable = btn.dataset.enable === 'true';
  if (enable) {
    btn.addEventListener('click', listener(enable));
  } else {
    confirm.addEventListener('click', listener(enable));
  }
}

function bindTabEvents() {
  $('.nav-tabs a').on('shown.bs.tab', function(event) {
    let targetNavItem = $(event.target).parent();
    let targetNavTabs = $(targetNavItem).parent();

    $(targetNavTabs).children(".nav-tab").removeClass("active");
    $(targetNavItem).addClass("active");
  });
}

function debounce(callback, wait) {
  let timeout;
  return (...args) => {
    clearTimeout(timeout);
    timeout = setTimeout(function() { callback.apply(this, args); }, wait);
  };
}

function formDataToJson(formData) {
  var object = {};
  formData.forEach((value, key) => {
    // Reflect.has in favor of: object.hasOwnProperty(key)
    if (!Reflect.has(object, key)) {
      object[key] = value;
      return;
    }
    if (!Array.isArray(object[key])) {
      object[key] = [object[key]];
    }
    object[key].push(value);
  });
  return object;
}

function getImageUrl(image, width = 'full') {
  if (!image.httpBaseUrl || !image.mimeType) {
    return image.uri;
  }
  const mimeExtensionMapping = {
    gif: 'gif',
    png: 'png',
  };
  const subMimeType = image.mimeType.split('/')[1];
  return `${image.httpBaseUrl}/full/${width}/0/default.${mimeExtensionMapping[subMimeType] ?? 'jpg'}`;
}

function formatDate(date, language, onlyDate = false) {
  /* used to output a date or date with time */
  if (!date) {
    return null;
  }
  const dateToFormat = date instanceof Date ? date : new Date(date);
  const options = {
    day: '2-digit',
    hour12: false,
    month: '2-digit',
    year: 'numeric'
  };
  if (onlyDate) {
    return dateToFormat.toLocaleDateString(language, options)
  }
  return dateToFormat.toLocaleString(language, {
    ...options,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
}

function formatStringArray(value) {
  /* used to output a list of strings as comma separated list */
  let html = '';
  for (var i = 0; i < value.length; i++) {
    html = html + value[i];
    if (i < value.length - 1) {
      html = html + ', ';
    }
  }
  return html;
}

function handleFetchErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

function moveEditButtonToNavbar() {
  /* used in view pages to move edit button to navbar if page is scrollable */
  var navbar = document.querySelector('.navbar');
  var editButton = document.getElementById('edit-button');
  var editButtonInNavbar = document.createElement('li');
  editButtonInNavbar.classList.add('border-left', 'ml-2', 'nav-item', 'pl-3');
  editButtonInNavbar.innerHTML = `<a class="border border-white btn btn-primary btn-sm" id="sticky-edit-button">${editButton.innerText}</a>`;
  var observer = new IntersectionObserver(
    (entry, _) => {
      var inView = entry[0].isIntersecting && entry[0].intersectionRatio >= 1;
      if (inView) {
        editButton.classList.add('visible');
        editButton.classList.remove('invisible');
        editButtonInNavbar.remove();
      } else {
        editButton.classList.add('invisible');
        editButton.classList.remove('visible');
        editButtonInNavbar
          .querySelector('a')
          .setAttribute('href', editButton.getAttribute('href'));
        navbar.querySelector('.navbar-nav').appendChild(editButtonInNavbar);
      }
    },
    {
      rootMargin: `-${navbar.offsetHeight}px 0px 0px 0px`,
      threshold: 1
    }
  );
  observer.observe(editButton);
}

function prependErrorIcon(element) {
  /* used in form pages to mark tabs with erroneous input */
  $(element).prepend('<i class="fas fa-exclamation-circle error mr-2"></i>');
}

function showMessage(cssClass, alertElementId, message) {
  $(alertElementId).removeClass("alert-success alert-danger alert-warning alert-info").addClass(cssClass);
  $(alertElementId).children('span:first-child').html(message);
  $(alertElementId).removeClass("d-none");
}