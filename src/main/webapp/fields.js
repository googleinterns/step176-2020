// Annotated fields have two names associated with them: a user-friendly name
// displayed in the UI and a backend API name used in API requests/responses
const AnnotatedFields = {
  ASSET_ID: {
    DISPLAY: 'Asset ID',
    API: 'annotatedAssetId'
  },
  LOCATION: {
    DISPLAY: 'Location',
    API: 'annotatedLocation'
  },
  USER: {
    DISPLAY: 'User',
    API: 'annotatedUser'
  }
};

function getAnnotatedFieldFromDisplay(display) {
  for (let field of Object.keys(AnnotatedFields)) {
    if (AnnotatedFields[field].DISPLAY == display) {
      return AnnotatedFields[field];
    }
  }
}

export {AnnotatedFields, getAnnotatedFieldFromDisplay};
