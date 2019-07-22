import React from 'react';
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
} from '../config/index';

const FormEditor = (props) => {
  if (props.document) {
    options.doc = options.schema.nodeFromJSON(props.document);
  }
  return (
    <>
      <Label className='font-weight-bold'>{props.type}</Label>
      <div className='border'>
        <Editor
          options={options}
          onChange={doc => {
            props.onUpdate(JSON.parse(JSON.stringify(doc)));
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
  )
};

export default FormEditor;
