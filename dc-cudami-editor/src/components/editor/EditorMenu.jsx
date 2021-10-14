import './EditorMenu.css'

import classNames from 'classnames'
import {DropdownItem, DropdownMenu, UncontrolledDropdown} from 'reactstrap'

const Button = ({dispatch, fullWidth, item, state}) => {
  const disabled = item.enable && !item.enable(state)
  return (
    <button
      className={classNames(
        'menu-button',
        fullWidth && ['full-width', 'text-left'],
        {
          active: item.active && item.active(state),
          disabled,
        },
      )}
      onClick={(evt) => {
        evt.preventDefault()
        if (!disabled && item.run) {
          item.run(state, dispatch)
        }
      }}
      title={item.title}
      type="button"
    >
      {item.content}
    </button>
  )
}

const ButtonGroup = ({items, view}) => {
  return (
    <span className="menu-group">
      {Object.entries(items).map(([key, item]) => {
        if (item.children) {
          return (
            <DropDown
              dispatch={view.dispatch}
              item={item}
              key={key}
              state={view.state}
            />
          )
        }
        return (
          <Button
            dispatch={view.dispatch}
            item={item}
            key={key}
            state={view.state}
          />
        )
      })}
    </span>
  )
}

const DropDown = ({dispatch, item, state}) => {
  return (
    <UncontrolledDropdown>
      <Button dispatch={dispatch} item={item} state={state} />
      <DropdownMenu className="m-0 p-0">
        {item.children.map((child, index) => {
          return (
            <DropdownItem className="p-0" key={index} tag="div">
              <Button
                fullWidth={true}
                dispatch={dispatch}
                item={child}
                state={state}
              />
            </DropdownItem>
          )
        })}
      </DropdownMenu>
    </UncontrolledDropdown>
  )
}

const EditorMenu = ({menu, view}) => (
  <div className="menu-bar">
    {Object.entries(menu).map(([key, value]) => (
      <ButtonGroup items={value} key={key} view={view} />
    ))}
  </div>
)

export default EditorMenu
