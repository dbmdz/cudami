import React from 'react'
import {
  Button,
  ButtonGroup,
  Card,
  CardBody,
  CardFooter,
  Col,
  Container,
  Nav,
  Navbar,
  NavbarBrand,
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
  FaPalette,
  FaPencilAlt,
  FaSitemap,
  FaUniversity,
} from 'react-icons/fa'
import {BrowserRouter as Router, Link, Route} from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.min.css'

import initI18n from './i18n'
import IdentifiableForm from './components/IdentifiableForm'
import PagedIdentifiableList from './components/PagedIdentifiableList'
import PagedRenderingTemplateList from './components/PagedRenderingTemplateList'
import RenderingTemplateForm from './components/RenderingTemplateForm'

const availableFormTypes = [
  'article',
  'collection',
  'corporateBody',
  'fileResource',
  'project',
  'topic',
  'webpage',
  'website',
]

// this is a mapping of parent types to possible child types
const availableAttachedListTypes = {
  collection: ['subcollection', 'digitalObject'],
  project: ['digitalObject'],
  webpage: ['webpage'],
  website: ['webpage'],
}

const availableRootListTypes = ['collection', 'website']

const getUiLocale = (searchParams) => {
  const query = new URLSearchParams(searchParams)
  return query.get('lang') ?? 'en'
}

const iconMapping = {
  article: FaNewspaper,
  collection: FaList,
  corporateBody: FaUniversity,
  digitalObject: FaCubes,
  fileResource: FaFile,
  project: FaIndustry,
  renderingTemplate: FaPalette,
  subcollection: FaList,
  topic: FaSitemap,
  webpage: FaGlobe,
  website: FaGlobe,
}

const FormCard = ({type}) => {
  const Icon = iconMapping[type]
  return (
    <Col md="3">
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
}

const ListCard = ({parentType, type}) => {
  const Icon = iconMapping[type]
  const label = parentType ? `${parentType} / ${type}` : `${type}`
  const link = parentType ? `/${parentType}/${type}/list` : `/${type}/list`
  return (
    <Col key={type} md="3">
      <Card className="mb-3 text-center">
        <Link className="stretched-link" to={link}>
          <Icon className="card-img-top mt-3" size="65" />
          <CardBody>{label}</CardBody>
        </Link>
      </Card>
    </Col>
  )
}

const StartPage = () => (
  <Container>
    <h1>Available components</h1>
    <h2>Form</h2>
    <h3>Identifiables</h3>
    <Row>
      {availableFormTypes.map((type) => (
        <FormCard key={type} type={type} />
      ))}
    </Row>
    <h3>Other</h3>
    <Row>
      <FormCard type="renderingTemplate" />
    </Row>
    <h2>List</h2>
    <h3>Root identifiables</h3>
    <Row>
      {availableRootListTypes.map((type) => (
        <ListCard key={type} type={type} />
      ))}
    </Row>
    <h3>Attached identifiables</h3>
    <Row>
      {Object.entries(availableAttachedListTypes).map(
        ([parentType, childTypes]) => {
          return childTypes.map((type) => (
            <ListCard
              key={`${parentType}-${type}`}
              parentType={parentType}
              type={type}
            />
          ))
        }
      )}
    </Row>
    <h3>Other</h3>
    <Row>
      <ListCard type="renderingTemplate" />
    </Row>
  </Container>
)

const App = () => {
  const uiLocale = getUiLocale(window.location.search)
  initI18n(uiLocale)
  return (
    <>
      <Navbar color="light" expand="md" fixed="top" light>
        <NavbarBrand href="/">
          <FaHome size="25" />
        </NavbarBrand>
        <Nav className="ml-auto" navbar />
      </Navbar>
      <Router>
        <Route component={StartPage} exact path="/" />
        <Route
          path={`/:type(${availableFormTypes.join('|')})/new`}
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
          path={`/:type(${availableFormTypes.join('|')})/edit`}
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
                enableChangeOfOrder={true}
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
        <Route
          path={`/:type(${availableRootListTypes.join('|')})/list`}
          render={({match}) => (
            <Container>
              <PagedIdentifiableList
                mockApi={true}
                showEdit={true}
                showNew={true}
                type={match.params.type}
                uiLocale={uiLocale}
              />
            </Container>
          )}
        />
        <Route
          path={'/renderingTemplate/new'}
          render={() => (
            <Container>
              <RenderingTemplateForm mockApi={true} />
            </Container>
          )}
        />
        <Route
          path={'/renderingTemplate/edit'}
          render={() => (
            <Container>
              <RenderingTemplateForm mockApi={true} uuid="mock" />
            </Container>
          )}
        />
        <Route
          path={'/renderingTemplate/list'}
          render={() => (
            <Container>
              <PagedRenderingTemplateList mockApi={true} />
            </Container>
          )}
        />
      </Router>
    </>
  )
}

export default App
