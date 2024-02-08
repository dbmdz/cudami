import {useTranslation} from 'react-i18next'
import {FaCircle} from 'react-icons/fa'

const PublicationStatus = ({publicationEnd, publicationStart}) => {
  const {t} = useTranslation()
  const publicationStartDate =
    !publicationStart || publicationStart instanceof Date
      ? publicationStart
      : new Date(publicationStart)
  const publicationEndDate =
    !publicationEnd || publicationEnd instanceof Date
      ? publicationEnd
      : new Date(publicationEnd)
  const now = Date.now()
  let color = ''
  let publicationStatus = ''
  if (!publicationStartDate || publicationStartDate.getTime() > now) {
    color = 'warning'
    publicationStatus = 'notYetPublished'
  } else if (
    publicationStartDate.getTime() <= now &&
    (!publicationEndDate || publicationEndDate.getTime() > now)
  ) {
    color = 'success'
    publicationStatus = 'currentlyPublished'
  } else if (
    publicationStartDate.getTime() <= now &&
    publicationEndDate.getTime() <= now
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
