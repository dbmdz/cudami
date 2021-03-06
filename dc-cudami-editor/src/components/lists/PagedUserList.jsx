import classNames from 'classnames'
import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaHashtag, FaPowerOff, FaToggleOff, FaToggleOn} from 'react-icons/fa'
import {Button, Card, CardBody, Col, Row, Table} from 'reactstrap'

import {changeUserStatus, typeToEndpointMapping} from '../../api'
import usePagination from '../../hooks/usePagination'
import DeactiveUserDialog from '../dialogs/DeactiveUserDialog'
import FeedbackMessage from '../FeedbackMessage'
import Pagination from '../Pagination'
import ActionButtons from './ActionButtons'

const PagedUserList = ({apiContextPath = '/'}) => {
  const type = 'user'
  const {
    content: users,
    numberOfPages,
    pageNumber,
    pageSize,
    totalElements,
    setContent,
    setPageNumber,
  } = usePagination(apiContextPath, type)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [feedbackMessage, setFeedbackMessage] = useState()
  const [userIndex, setUserIndex] = useState()
  const {t} = useTranslation()
  const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
  const changeStatus = async (index = userIndex) => {
    const user = users[index]
    const successful = await changeUserStatus(
      apiContextPath,
      user.uuid,
      !user.enabled
    )
    if (!successful) {
      return setFeedbackMessage({
        color: 'danger',
        key: user.enabled
          ? 'userNotDeactivatedSuccessfully'
          : 'userNotActivatedSuccessfully',
        values: {email: user.email},
      })
    }
    users[index].enabled = !user.enabled
    setContent(users)
    setFeedbackMessage({
      color: 'success',
      key: user.enabled
        ? 'userActivatedSuccessfully'
        : 'userDeactivatedSuccessfully',
      values: {email: user.email},
    })
  }
  return (
    <>
      <Row>
        <Col>
          <h1>{t(`types:${type}_plural`)}</h1>
        </Col>
        <Col className="text-right">
          <Button href={`${apiContextPath}${typeToEndpointMapping[type]}/new`}>
            {t('new')}
          </Button>
        </Col>
      </Row>
      <hr />
      {feedbackMessage && (
        <FeedbackMessage
          className="mb-2"
          message={feedbackMessage}
          onClose={() => setFeedbackMessage(undefined)}
        />
      )}
      <Card className="border-top-0">
        <CardBody>
          <Pagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            totalElements={totalElements}
            type={type}
          />
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <thead>
              <tr>
                <th className="text-right">
                  <FaHashtag />
                </th>
                <th className="text-center">{t('lastname')}</th>
                <th className="text-center">{t('firstname')}</th>
                <th className="text-center">{`${t('username')} / ${t(
                  'email'
                )}`}</th>
                <th className="text-center">{t('roles')}</th>
                <th className="text-center">{t('status')}</th>
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {users.map(
                ({email, enabled, firstname, lastname, roles, uuid}, index) => (
                  <tr key={uuid}>
                    <td className="text-right">
                      {index + 1 + pageNumber * pageSize}
                    </td>
                    <td>{lastname}</td>
                    <td>{firstname}</td>
                    <td>
                      <a href={`${viewBaseUrl}/${uuid}`}>{email}</a>
                    </td>
                    <td>{roles.join(', ')}</td>
                    <td className="text-center">
                      <FaPowerOff
                        className={classNames({
                          'text-danger': !enabled,
                          'text-success': enabled,
                        })}
                        title={enabled ? t('activated') : t('deactivated')}
                      />
                    </td>
                    <td className="text-center">
                      <ActionButtons
                        showEdit
                        viewUrl={`${viewBaseUrl}/${uuid}`}
                      >
                        <Button
                          className="p-1"
                          color="link"
                          onClick={() => {
                            setUserIndex(index)
                            if (enabled) {
                              setDialogOpen(true)
                            } else {
                              changeStatus(index)
                            }
                          }}
                        >
                          {enabled ? <FaToggleOff /> : <FaToggleOn />}
                        </Button>
                      </ActionButtons>
                    </td>
                  </tr>
                )
              )}
            </tbody>
          </Table>
          <Pagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            position="under"
            showTotalElements={false}
            totalElements={totalElements}
            type={type}
          />
        </CardBody>
      </Card>
      <DeactiveUserDialog
        email={users[userIndex]?.email}
        isOpen={dialogOpen}
        onConfirm={() => changeStatus()}
        toggle={() => {
          setDialogOpen(false)
        }}
      />
    </>
  )
}

export default PagedUserList
