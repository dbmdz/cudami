import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaHashtag} from 'react-icons/fa'
import {Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'

import {getDefaultLanguage} from '../../api'
import usePagination from '../../hooks/usePagination'
import LanguageTab from '../LanguageTab'
import ListSearch from '../ListSearch'
import Pagination from '../Pagination'
import {formatDate} from '../utils'

const PagedLicenseList = ({apiContextPath = '/', uiLocale}) => {
  const type = 'license'
  const {
    content: licenses,
    numberOfPages,
    pageNumber,
    pageSize,
    totalElements,
    setPageNumber,
    setSearchTerm: executeSearch,
  } = usePagination(apiContextPath, type, [{property: 'url'}])
  const [defaultLanguage, setDefaultLanguage] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  useEffect(() => {
    getDefaultLanguage(apiContextPath).then((defaultLanguage) =>
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
                <th className="text-center">{t('acronym')}</th>
                <th className="text-center">URL</th>
                <th className="text-center">{t('lastModified')}</th>
              </tr>
            </thead>
            <tbody>
              {licenses.map(
                ({acronym, label, lastModified, url, uuid}, index) => (
                  <tr key={uuid}>
                    <td className="text-right">
                      {index + 1 + pageNumber * pageSize}
                    </td>
                    <td>{label?.[defaultLanguage]}</td>
                    <td>{acronym}</td>
                    <td>
                      <a href={url} rel="noreferrer" target="_blank">
                        {url}
                      </a>
                    </td>
                    <td className="text-center">
                      {formatDate(lastModified, uiLocale)}
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

export default PagedLicenseList
