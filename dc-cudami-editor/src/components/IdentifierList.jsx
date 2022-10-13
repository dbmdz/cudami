import sortBy from 'lodash-es/sortBy'
import {Badge, List, ListInlineItem} from 'reactstrap'

const IdentifierList = ({identifiers = [], identifierTypes = []}) =>
  identifiers.length > 0 ? (
    <List className="mb-0" type="inline">
      {sortBy(identifiers, ['namespace']).map(({id, namespace}) => {
        const identifierType = identifierTypes.find(
          (identifierType) => identifierType.namespace === namespace,
        )
        return (
          <ListInlineItem key={`${namespace}:${id}`}>
            <Badge
              className="border border-dark p-2"
              color="light"
              pill
              title={identifierType?.label}
            >{`${namespace}:${id}`}</Badge>
          </ListInlineItem>
        )
      })}
    </List>
  ) : (
    '---'
  )

export default IdentifierList
