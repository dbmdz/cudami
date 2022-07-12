import sortBy from 'lodash/sortBy'
import {Badge, List, ListInlineItem} from 'reactstrap'

const IdentifierList = ({identifiers = [], identifierTypes = []}) =>
  identifiers.length > 0 ? (
    <List className="mb-0" type="inline">
      {sortBy(identifiers, ['namespace']).map(({id, namespace}) => (
        <ListInlineItem key={`${namespace}:${id}`}>
          <Badge className="pb-1 pt-1" color="primary" pill>{`${
            identifierTypes.find(
              (identifierType) => identifierType.namespace === namespace,
            )?.label ?? namespace
          }: ${id}`}</Badge>
        </ListInlineItem>
      ))}
    </List>
  ) : null

export default IdentifierList
