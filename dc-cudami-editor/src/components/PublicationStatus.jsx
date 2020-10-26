import React from 'react'
import {Badge} from 'reactstrap'
import {useTranslation} from 'react-i18next'

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
    <Badge
      className="d-inline-block mt-1"
      color={color}
      pill
      style={{height: '15px', width: '15px'}}
      title={t(`publicationStatus.${publicationStatus}`)}
    />
  )
}

export default PublicationStatus
