import ReactDOM from 'react-dom'

import {loadRootIdentifiables} from '../api'
import IdentifierList from '../components/IdentifierList'

export default async function ({apiContextPath, id, identifiers}) {
  const pageSize = 20
  const {content: identifierTypes} = await loadRootIdentifiables(
    apiContextPath,
    'identifierType',
    {
      pageNumber: 0,
      pageSize,
      sorting: {
        orders: [{property: 'namespace'}, {property: 'uuid'}],
      },
    },
  )
  ReactDOM.render(
    <IdentifierList
      identifiers={identifiers}
      identifierTypes={identifierTypes}
    />,
    document.getElementById(id),
  )
}
