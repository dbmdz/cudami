import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaHashtag} from 'react-icons/fa'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'

import {loadDefaultLanguage, typeToEndpointMapping} from '../../api'
import usePagination from '../../hooks/usePagination'
import LanguageTab from '../LanguageTab'
import ListSearch from '../ListSearch'
import Pagination from '../Pagination'
import {formatDate} from '../utils'
import ActionButtons from './ActionButtons'

const PagedRenderingTemplateList = ({apiContextPath = '/', uiLocale}) => {
  const type = 'renderingTemplate'
  const {
    content: templates,
    numberOfPages,
    pageNumber,
    pageSize,
    totalElements,
    setPageNumber,
    setSearchTerm: executeSearch,
  } = usePagination(apiContextPath, type, [
    {property: 'name'},
    {property: 'uuid'},
  ])
  const [defaultLanguage, setDefaultLanguage] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  useEffect(() => {
    loadDefaultLanguage(apiContextPath).then((defaultLanguage) =>
      setDefaultLanguage(defaultLanguage),
    )
  }, [])
  const {t} = useTranslation()
  return (
    <>
      <Row>
        <Col>
          {/* We want to force the usage of the plural form here (0 as count activates it) */}
          <h1>{t(`types:${type}`, {count: 0})}</h1>
        </Col>
        <Col className="text-right">
          <Button
            color="primary"
            href={`${apiContextPath}${typeToEndpointMapping[type]}/new`}
          >
            {t('new')}
          </Button>
        </Col>
      </Row>
      <hr />
      <Nav tabs>
        <LanguageTab
          activeLanguage={defaultLanguage}
          language={defaultLanguage}
          toggle={() => {}}
        />
      </Nav>
      <Card className="border-top-0">
        <CardBody>
          <div className="d-flex justify-content-between">
            <Pagination
              changePage={({selected}) => setPageNumber(selected)}
              numberOfPages={numberOfPages}
              pageNumber={pageNumber}
              totalElements={totalElements}
              type={type}
            />
            <ListSearch
              isHighlighted={totalElements === 0 && searchTerm}
              onChange={(value) => setSearchTerm(value)}
              onSubmit={() => executeSearch(searchTerm)}
              value={searchTerm}
            />
          </div>
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <thead>
              <tr>
                <th className="text-right">
                  <FaHashtag />
                </th>
                <th className="text-center">{t('label')}</th>
                <th className="text-center">{t('description')}</th>
                <th className="text-center">{t('name')}</th>
                <th className="text-center">{t('lastModified')}</th>
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {templates.map(
                ({description, label, lastModified, name, uuid}, index) => (
                  <tr key={uuid}>
                    <td className="text-right">
                      {index + 1 + pageNumber * pageSize}
                    </td>
                    <td>{label?.[defaultLanguage]}</td>
                    <td>{description?.[defaultLanguage]}</td>
                    <td>{name}</td>
                    <td className="text-center">
                      {formatDate(lastModified, uiLocale)}
                    </td>
                    <td className="text-center">
                      <ActionButtons
                        editUrl={`${apiContextPath}${typeToEndpointMapping[type]}/${uuid}/edit`}
                        showEdit
                        showView={false}
                      />
                    </td>
                  </tr>
                ),
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
    </>
  )
}

export default PagedRenderingTemplateList
