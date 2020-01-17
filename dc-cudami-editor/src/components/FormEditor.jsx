import React, {Component} from 'react'
import {Editor, Floater, MenuBar} from '@aeaton/react-prosemirror'
import {Label} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './FormEditor.css'
import FormEditorMenu from './FormEditorMenu'
import {menu, options} from '../config'

class FormEditor extends Component {
  constructor(props) {
    super(props)
    this.editorOptions = {
      ...options,
      doc: props.document
        ? options.schema.nodeFromJSON(props.document)
        : undefined,
    }
  }

  render() {
    const {onUpdate, restrictedMenu, t, type} = this.props
    let translatedMenu = menu(t)
    if (restrictedMenu) {
      translatedMenu = {
        marks: translatedMenu.marks,
      }
    }
    return (
      <>
        <Label className="font-weight-bold">{t(type)}</Label>
        <div className="text-editor">
          <Editor
            options={this.editorOptions}
            onChange={doc => {
              onUpdate(JSON.parse(JSON.stringify(doc)))
            }}
            render={({editor, view}) => (
              <>
                <FormEditorMenu menu={translatedMenu} view={view} />

                <Floater view={view}>
                  <MenuBar menu={{marks: translatedMenu.marks}} view={view} />
                </Floater>

                {editor}
              </>
            )}
          />
        </div>
      </>
    )
  }
}

export default withTranslation()(FormEditor)
