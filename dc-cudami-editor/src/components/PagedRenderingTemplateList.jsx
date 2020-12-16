import React, {useEffect, useState} from 'react'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import LanguageTab from './LanguageTab'
import ListButtons from './ListButtons'
import ListPagination from './ListPagination'
import {
  loadDefaultLanguage,
  loadIdentifiables,
  typeToEndpointMapping,
} from '../api'

const loadData = async (context, mock, pageNumber, pageSize = 20) => {
  const defaultLanguage = await loadDefaultLanguage(context, mock)
  const {content, totalElements} = await loadIdentifiables(
    context,
    mock,
    'renderingTemplate',
    pageNumber,
    pageSize
  )
  return {
    defaultLanguage,
    numberOfPages: Math.ceil(totalElements / pageSize),
    templates: content,
    totalElements,
  }
}

const PagedRenderingTemplateList = ({
  apiContextPath = '/',
  mockApi = false,
}) => {
  const [defaultLanguage, setDefaultLanguage] = useState('')
  const [numberOfPages, setNumberOfPages] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [templates, setTemplates] = useState([])
  const [totalElements, setTotalElements] = useState(0)
  useEffect(() => {
    loadData(apiContextPath, mockApi, pageNumber).then(
      ({defaultLanguage, numberOfPages, templates, totalElements}) => {
        setDefaultLanguage(defaultLanguage)
        setNumberOfPages(numberOfPages)
        setTemplates(templates)
        setTotalElements(totalElements)
      }
    )
  }, [pageNumber])
  const {t} = useTranslation()
  const type = 'renderingTemplate'
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

// const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
