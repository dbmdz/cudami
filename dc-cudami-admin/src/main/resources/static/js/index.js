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
    var urlParams = editUrl.length > 1 ? new URLSearchParams(editUrl[1]) : new URLSearchParams('')
    urlParams.set('activeLanguage', selectedLanguage);
    editUrl = [editUrl[0], urlParams.toString()]
    $('#edit-button').attr('href', editUrl.join('?'))
  })
}
