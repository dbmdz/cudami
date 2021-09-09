const IdentifierList = ({identifiers, identifierTypes}) => (
  <ul className="list-inline mb-0">
    {identifiers.map(({id, namespace}) => (
      <li className="list-inline-item" key={`${namespace}:${id}`}>{`${
        identifierTypes.find(
          (identifierType) => identifierType.namespace === namespace,
        )?.label ?? namespace
      }: ${id}`}</li>
    ))}
  </ul>
)

export default IdentifierList
