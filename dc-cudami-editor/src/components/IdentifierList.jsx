import {List, ListInlineItem} from 'reactstrap'

const IdentifierList = ({identifiers = [], identifierTypes = []}) =>
  identifiers.length > 0 ? (
    <List className="mb-0" type="inline">
      {identifiers.map(({id, namespace}) => (
        <ListInlineItem key={`${namespace}:${id}`}>{`${
          identifierTypes.find(
            (identifierType) => identifierType.namespace === namespace,
          )?.label ?? namespace
        }: ${id}`}</ListInlineItem>
      ))}
    </List>
  ) : null

export default IdentifierList
