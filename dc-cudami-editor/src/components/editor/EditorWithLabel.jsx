import './EditorWithLabel.css'

import {Editor} from '@aeaton/react-prosemirror'
import {useTranslation} from 'react-i18next'
import {Label} from 'reactstrap'

import {menu, options} from '../../config'
import EditorMenu from './EditorMenu'

const EditorWithLabel = ({
  children,
  document: doc,
  onUpdate,
  restrictedMenu = false,
  type,
}) => {
  const {t} = useTranslation()
  const translatedMenu = menu(t)
  return (
    <>
      <Label className="font-weight-bold">{t(type)}</Label>
      {children}
      <div className="clearfix text-editor">
        <Editor
          options={{
            ...options,
            doc: doc && options.schema.nodeFromJSON(doc),
          }}
          onChange={(doc) => {
            onUpdate(JSON.parse(JSON.stringify(doc)))
          }}
          render={({editor, view}) => (
            <>
              <EditorMenu
                menu={
                  restrictedMenu
                    ? {
                        history: translatedMenu.history,
                        marks: translatedMenu.marks,
                      }
                    : translatedMenu
                }
                view={view}
              />
              <div className="text-area">{editor}</div>
            </>
          )}
        />
      </div>
    </>
  )
}

export default EditorWithLabel
