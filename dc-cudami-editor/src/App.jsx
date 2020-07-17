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
  FaFile,
  FaFolderPlus,
  FaGlobe,
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

const availableTypes = [
  'article',
  'collection',
  'corporation',
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
    corporation: FaUniversity,
    fileResource: FaFile,
    project: FaIndustry,
    subtopic: FaSitemap,
    topic: FaSitemap,
    webpage: FaGlobe,
    website: FaGlobe,
  }
  return (
    <Container className="mt-5">
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
    </Container>
  )
}

const App = () => {
  initI18n(getUiLocale(window.location.search))
  return (
    <Router>
      <Route component={StartPage} exact={true} path="/" />
      <Route
        path={`/:type(${availableTypes.join('|')})/new`}
        render={({match}) => (
          <IdentifiableForm
            activeLanguage="en"
            debug={true}
            mockApi={true}
            type={match.params.type}
          />
        )}
      />
      <Route
        path={`/:type(${availableTypes.join('|')})/edit`}
        render={({match}) => (
          <IdentifiableForm
            activeLanguage="en"
            debug={true}
            existingLanguages={['en', 'de']}
            mockApi={true}
            type={match.params.type}
            uuid="mock"
          />
        )}
      />
    </Router>
  )
}

export default App
