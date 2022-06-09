import {List, ListInlineItem} from 'reactstrap'

const IdentifierList = ({identifiers, identifierTypes}) => (
  <List className="mb-0" type="inline">
    {identifiers.map(({id, namespace}) => (
      <ListInlineItem key={`${namespace}:${id}`}>{`${
        identifierTypes.find(
          (identifierType) => identifierType.namespace === namespace,
        )?.label ?? namespace
      }: ${id}`}</ListInlineItem>
    ))}
  </List>
)

export default IdentifierList
