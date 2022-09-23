function activateExternalLinks() {
  $('a[href]')
    .filter(function () {
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

function addLanguageChangeHandler() {
  $('.language-switcher').on('click', function () {
    // get the href attribute and cut off the leading hash to get the selected language
    var selectedLanguage = $(this).attr('href').slice(1)
    var editUrl = $('#edit-button').attr('href').split('?')
    var urlParams =
      editUrl.length > 1
        ? new URLSearchParams(editUrl[1])
        : new URLSearchParams('')
    urlParams.set('activeLanguage', selectedLanguage)
    editUrl = [editUrl[0], urlParams.toString()]
    $('#edit-button').attr('href', editUrl.join('?'))
  })
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

function addUserStatusChangeHandler(url) {
  const listener = function (enabled) {
    return async function (_evt) {
      try {
        const response = await fetch(url, {
          body: JSON.stringify({enabled}),
          headers: {
            'Content-Type': 'application/json',
          },
          method: 'PATCH',
        })
        if (!response.ok) {
          throw new Error('Error during change of user status')
        }
        window.location.reload()
      } catch (err) {
        console.error(err)
      }
    }
  }
  const btn = document.querySelector('input.user-status-toggle')
  const confirm = document.querySelector('a#confirm')
  const enable = btn.dataset.enable === 'true'
  if (enable) {
    btn.addEventListener('click', listener(enable))
  } else {
    confirm.addEventListener('click', listener(enable))
  }
}
