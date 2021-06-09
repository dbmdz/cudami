import {useTranslation} from 'react-i18next'
import {FaCircle} from 'react-icons/fa'

const PublicationStatus = ({publicationEnd, publicationStart}) => {
  const {t} = useTranslation()
  let color = ''
  const now = Date.now()
  let publicationStatus = ''
  if (!publicationStart || publicationStart.getTime() > now) {
    color = 'warning'
    publicationStatus = 'notYetPublished'
  } else if (
    publicationStart.getTime() <= now &&
    (!publicationEnd || publicationEnd.getTime() > now)
  ) {
    color = 'success'
    publicationStatus = 'currentlyPublished'
  } else if (
    publicationStart.getTime() <= now &&
    publicationEnd.getTime() <= now
  ) {
    color = 'danger'
    publicationStatus = 'noLongerPublished'
  }
  return (
    <FaCircle
      className={`text-${color}`}
      title={t(`publicationStatus:${publicationStatus}`)}
    />
  )
}

export default PublicationStatus
