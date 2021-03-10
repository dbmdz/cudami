import './EditorWithLabel.css'

import {Editor} from '@aeaton/react-prosemirror'
import React, {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {Label} from 'reactstrap'

import {menu, options} from '../../config'
import EditorMenu from './EditorMenu'

class EditorWithLabel extends Component {
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
    const {children, onUpdate, restrictedMenu, t, type} = this.props
    let translatedMenu = menu(t)
    if (restrictedMenu) {
      translatedMenu = {
        marks: translatedMenu.marks,
      }
    }
    return (
      <>
        <Label className="font-weight-bold">{t(type)}</Label>
        {children}
        <div className="clearfix text-editor">
          <Editor
            options={this.editorOptions}
            onChange={(doc) => {
              onUpdate(JSON.parse(JSON.stringify(doc)))
            }}
            render={({editor, view}) => (
              <>
                <EditorMenu menu={translatedMenu} view={view} />
                <div className="text-area">{editor}</div>
              </>
            )}
          />
        </div>
      </>
    )
  }
}

export default withTranslation()(EditorWithLabel)
