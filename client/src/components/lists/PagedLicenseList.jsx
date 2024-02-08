import '../../polyfills'

import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaHashtag} from 'react-icons/fa'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {useContext} from 'use-context-selector'

import {typeToEndpointMapping} from '../../api'
import usePagination from '../../hooks/usePagination'
import {setActiveLanguage} from '../../state/actions'
import {getActiveLanguage, getExistingLanguages} from '../../state/selectors'
import {Context} from '../../state/Store'
import LanguageTab from '../LanguageTab'
import ListSearch from '../ListSearch'
import Pagination from '../Pagination'
import {formatDate} from '../utils'
import ActionButtons from './ActionButtons'

const PagedLicenseList = () => {
  const type = 'license'
  const activeLanguage = getActiveLanguage()
  const existingLanguages = getExistingLanguages()
  const {apiContextPath, dispatch, uiLocale} = useContext(Context)
  const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
  const {
    content: licenses,
    numberOfPages,
    pageNumber,
    pageSize,
    totalElements,
    setPageNumber,
    setSearchTerm: executeSearch,
  } = usePagination(apiContextPath, type, [{property: 'url'}])
  const [searchTerm, setSearchTerm] = useState('')
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
        {existingLanguages.length > 1 &&
          existingLanguages.map((language) => (
            <LanguageTab
              activeLanguage={activeLanguage}
              key={language}
              language={language}
              toggle={(activeLanguage) =>
                dispatch(setActiveLanguage(activeLanguage))
              }
            />
          ))}
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
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {licenses.map(
                ({acronym, label, lastModified, url, uuid}, index) => (
                  <tr key={uuid}>
                    <td className="text-right">
                      {index + 1 + pageNumber * pageSize}
                    </td>
                    <td>{label?.[activeLanguage]}</td>
                    <td>{acronym}</td>
                    <td>
                      <a href={url} rel="noreferrer" target="_blank">
                        {url}
                      </a>
                    </td>
                    <td className="text-center">
                      {formatDate(lastModified, uiLocale)}
                    </td>
                    <td className="text-center">
                      <ActionButtons
                        showEdit={true}
                        viewUrl={`${viewBaseUrl}/${uuid}`}
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

export default PagedLicenseList
