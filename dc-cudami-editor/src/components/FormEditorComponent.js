import React from 'react';
import { Editor, Floater, MenuBar } from '@aeaton/react-prosemirror';
import { options, menu } from '@aeaton/react-prosemirror-config-default';

const FormEditorComponent = (props) => {
  if (props.description) {
    options.doc = options.schema.nodeFromJSON(props.description);
  }
  return (
    <>
      <Editor
        options={options}
        onChange={doc => {
          props.updateDocument(JSON.parse(JSON.stringify(doc)));
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
    </>
  )
};

export default FormEditorComponent;
