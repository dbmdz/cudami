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
    $('#edit-button, #sticky-edit-button').attr('href', editUrl.join('?'))
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

function moveEditButtonToNavbar() {
  var navbar = document.querySelector('.navbar')
  var editButton = document.getElementById('edit-button')
  var editButtonInNavbar = document.createElement('li')
  editButtonInNavbar.classList.add('border-left', 'ml-2', 'nav-item', 'pl-3')
  editButtonInNavbar.innerHTML = `<a class="btn btn-secondary" id="sticky-edit-button">${editButton.innerText}</a>`
  var observer = new IntersectionObserver(
    (entry, _) => {
      var inView = entry[0].isIntersecting && entry[0].intersectionRatio >= 1
      if (inView) {
        editButton.classList.add('visible')
        editButton.classList.remove('invisible')
        editButtonInNavbar.remove()
      } else {
        editButton.classList.add('invisible')
        editButton.classList.remove('visible')
        editButtonInNavbar
          .querySelector('a')
          .setAttribute('href', editButton.getAttribute('href'))
        navbar.querySelector('.navbar-nav').appendChild(editButtonInNavbar)
      }
    },
    {
      rootMargin: `-${navbar.offsetHeight}px 0px 0px 0px`,
      threshold: 1,
    },
  )
  observer.observe(editButton)
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
