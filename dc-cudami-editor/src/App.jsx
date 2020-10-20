import React from 'react'
import {
  Button,
  ButtonGroup,
  Card,
  CardBody,
  CardFooter,
  Col,
  Container,
  Row,
} from 'reactstrap'
import {
  FaCubes,
  FaFile,
  FaFolderPlus,
  FaGlobe,
  FaHome,
  FaIndustry,
  FaList,
  FaNewspaper,
  FaPencilAlt,
  FaSitemap,
  FaUniversity,
} from 'react-icons/fa'
import {BrowserRouter as Router, Link, Route} from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.min.css'

import initI18n from './i18n'
import IdentifiableForm from './components/IdentifiableForm'
import PagedIdentifiableList from './components/PagedIdentifiableList'

const availableListTypes = {
  collection: ['subcollection', 'digitalObject'],
  project: ['digitalObject'],
}

const availableTypes = [
  'article',
  'collection',
  'corporateBody',
  'fileResource',
  'project',
  'subtopic',
  'topic',
  'webpage',
  'website',
]

const getUiLocale = (searchParams) => {
  const query = new URLSearchParams(searchParams)
  return query.get('lang') ?? 'en'
}

const StartPage = () => {
  const iconMapping = {
    article: FaNewspaper,
    collection: FaList,
    corporateBody: FaUniversity,
    digitalObject: FaCubes,
    fileResource: FaFile,
    project: FaIndustry,
    subcollection: FaList,
    subtopic: FaSitemap,
    topic: FaSitemap,
    webpage: FaGlobe,
    website: FaGlobe,
  }
  return (
    <Container>
      <h1>Editor</h1>
      <Row>
        {availableTypes.map((type) => {
          const Icon = iconMapping[type]
          return (
            <Col key={type} md="3">
              <Card className="mb-3 text-center">
                <Icon className="card-img-top mt-3" size="65" />
                <CardBody>{type}</CardBody>
                <CardFooter className="p-0">
                  <ButtonGroup>
                    <Button color="light">
                      <Link className="stretched-link" to={`/${type}/new`}>
                        <FaFolderPlus />
                      </Link>
                    </Button>
                    <Button color="light">
                      <Link className="stretched-link" to={`/${type}/edit`}>
                        <FaPencilAlt />
                      </Link>
                    </Button>
                  </ButtonGroup>
                </CardFooter>
              </Card>
            </Col>
          )
        })}
      </Row>
      <h1>List</h1>
      <Row>
        {Object.keys(availableListTypes).map((parentType) => {
          return availableListTypes[parentType].map((type) => {
            const Icon = iconMapping[type]
            return (
              <Col key={type} md="3">
                <Card className="mb-3 text-center">
                  <Link
                    className="stretched-link"
                    to={`/${parentType}/${type}/list`}
                  >
                    <Icon className="card-img-top mt-3" size="65" />
                    <CardBody>
                      {parentType} / {type}
                    </CardBody>
                  </Link>
                </Card>
              </Col>
            )
          })
        })}
      </Row>
    </Container>
  )
}

const App = () => {
  const uiLocale = getUiLocale(window.location.search)
  initI18n(uiLocale)
  return (
    <>
      <Container className="mt-3">
        <a href="/">
          <FaHome size="25" />
        </a>
      </Container>
      <Router>
        <Route component={StartPage} exact={true} path="/" />
        <Route
          path={`/:type(${availableTypes.join('|')})/new`}
          render={({match}) => (
            <Container>
              <IdentifiableForm
                activeLanguage="en"
                mockApi={true}
                type={match.params.type}
                uiLocale={uiLocale}
              />
            </Container>
          )}
        />
        <Route
          path={`/:type(${availableTypes.join('|')})/edit`}
          render={({match}) => (
            <Container>
              <IdentifiableForm
                activeLanguage="en"
                existingLanguages={['en', 'de']}
                mockApi={true}
                type={match.params.type}
                uiLocale={uiLocale}
                uuid="mock"
              />
            </Container>
          )}
        />
        <Route
          path={`/:parentType/:type/list`}
          render={({match}) => (
            <Container>
              <PagedIdentifiableList
                enableAdd={true}
                enableMove={true}
                enableRemove={true}
                mockApi={true}
                parentType={match.params.parentType}
                showEdit={true}
                showNew={true}
                type={match.params.type}
                uiLocale={uiLocale}
              />
            </Container>
          )}
        />
      </Router>
    </>
  )
}

export default App
