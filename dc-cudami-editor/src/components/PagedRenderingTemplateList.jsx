import React, {useEffect, useState} from 'react'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import LanguageTab from './LanguageTab'
import ListButtons from './ListButtons'
import ListPagination from './ListPagination'
import {loadDefaultLanguage, typeToEndpointMapping} from '../api'
import usePagination from '../hooks/usePagination'

const PagedRenderingTemplateList = ({
  apiContextPath = '/',
  mockApi = false,
}) => {
  const type = 'renderingTemplate'
  const {
    content: templates,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  } = usePagination(apiContextPath, mockApi, type)
  const [defaultLanguage, setDefaultLanguage] = useState('')
  useEffect(() => {
    loadDefaultLanguage(apiContextPath, mockApi).then((defaultLanguage) =>
      setDefaultLanguage(defaultLanguage)
    )
  }, [])
  const {t} = useTranslation()
  return (
    <>
      <Row>
        <Col>
          <h1>{t('renderingTemplates')}</h1>
        </Col>
        <Col className="text-right">
          <Button href={`${apiContextPath}${typeToEndpointMapping[type]}/new`}>
            {t('new')}
          </Button>
        </Col>
      </Row>
      <Row>
        <Col>
          <hr />
        </Col>
      </Row>
      <Nav tabs>
        <LanguageTab
          activeLanguage={defaultLanguage}
          language={defaultLanguage}
          toggle={() => {}}
        />
      </Nav>
      <Card className="border-top-0">
        <CardBody>
          <ListPagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            totalElements={totalElements}
            type={type}
          />
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <thead>
              <tr>
                <th className="text-center">{t('label')}</th>
                <th className="text-center">{t('description')}</th>
                <th className="text-center">{t('name')}</th>
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {templates.map(({description, label, name, uuid}) => (
                <tr key={uuid}>
                  <td>{label?.[defaultLanguage]}</td>
                  <td>{description?.[defaultLanguage]}</td>
                  <td>{name}</td>
                  <td className="text-center">
                    <ListButtons
                      editUrl={`${apiContextPath}${typeToEndpointMapping[type]}/${uuid}/edit`}
                      showEdit
                      showView={false}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <ListPagination
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
