import './EditorMenu.css'

import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {DropdownItem, DropdownMenu, UncontrolledDropdown} from 'reactstrap'

const Button = ({
  dispatch,
  fullWidth,
  item: {active, content, enable, run, title},
  state,
}) => {
  const disabled = enable && !enable(state)
  return (
    <button
      className={classNames(
        'menu-button',
        fullWidth && ['full-width', 'text-left'],
        {
          active: active && active(state),
          disabled,
        },
      )}
      onClick={(evt) => {
        evt.preventDefault()
        if (!disabled && run) {
          run(state, dispatch)
        }
      }}
      title={title}
      type="button"
    >
      {content}
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
