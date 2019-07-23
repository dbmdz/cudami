import React, { Component } from 'react';
import {
  Editor,
  Floater,
  MenuBar
} from '@aeaton/react-prosemirror';
import {
  Label
} from 'reactstrap';

import {
  menu,
  options
} from '../config';

class FormEditor extends Component {
  constructor (props) {
    super(props);
    this.editorOptions = {
      ...options,
      doc: props.document ? options.schema.nodeFromJSON(props.document) : undefined
    };
  }

  render(){
    return (
      <>
        <Label className='font-weight-bold'>{this.props.type}</Label>
        <div className='border'>
          <Editor
            options={this.editorOptions}
            onChange={doc => {
              this.props.onUpdate(JSON.parse(JSON.stringify(doc)));
            }}
            render={({ editor, view }) => (
              <>
                <MenuBar menu={menu} view={view} />

                <Floater view={view}>
                  <MenuBar menu={{ marks: menu.marks }} view={view} />
                </Floater>

                {editor}
              </>
            )}
          />
        </div>
      </>
    );
  }
}

export default FormEditor;
