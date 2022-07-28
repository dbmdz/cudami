import './EditorMenu.css'

import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {DropdownItem, DropdownMenu, UncontrolledDropdown} from 'reactstrap'

const Button = ({
  dispatch,
  fullWidth,
  item: {
    active,
    content,
    contentActive,
    contentInactive,
    enable,
    run,
    titleKey,
    titleKeyActive,
    titleKeyInactive,
  },
  state,
}) => {
  const {t} = useTranslation()
  const isActive = active && active(state)
  const disabled = enable && !enable(state)
  // Check if there are different contents for active and inactive state
  if (contentActive && contentInactive) {
    if (isActive) {
      content = contentActive
    } else {
      content = contentInactive
    }
  }
  // Check if there are different titles for active and inactive state
  if (titleKeyActive && titleKeyInactive) {
    if (isActive) {
      titleKey = titleKeyActive
    } else {
      titleKey = titleKeyInactive
    }
  }
  return (
    <button
      className={classNames(
        'menu-button',
        fullWidth && ['full-width', 'text-left'],
        {
          active: isActive,
          disabled,
        },
      )}
      onClick={(evt) => {
        evt.preventDefault()
        if (!disabled && run) {
          run(state, dispatch)
        }
      }}
      title={t(`editor:${titleKey}`)}
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
