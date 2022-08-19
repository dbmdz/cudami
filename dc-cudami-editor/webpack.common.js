const path = require('path')
const _ = require('lodash')
const TerserPlugin = require('terser-webpack-plugin')

module.exports = {
  entry: {
    IdentifiableEditor: './src/lib/IdentifiableEditor.jsx',
    IdentifiableList: './src/lib/IdentifiableList.jsx',
    IdentifierList: './src/lib/IdentifierList.jsx',
    IdentifierTypeEditor: './src/lib/IdentifierTypeEditor.jsx',
    IdentifierTypeList: './src/lib/IdentifierTypeList.jsx',
    LicenseEditor: './src/lib/LicenseEditor.tsx',
    LicenseList: './src/lib/LicenseList.jsx',
    RenderingTemplateEditor: './src/lib/RenderingTemplateEditor.jsx',
    RenderingTemplateList: './src/lib/RenderingTemplateList.jsx',
    UrlAliasesList: './src/lib/UrlAliasesList.jsx',
    UserEditor: './src/lib/UserEditor.jsx',
    UserList: './src/lib/UserList.jsx',
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        exclude: /node_modules/,
        test: /\.tsx?$/,
        use: 'ts-loader',
      },
      {
        exclude: /(node_modules)/,
        test: /\.jsx?$/,
        use: ['babel-loader'],
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/,
        use: {
          loader: 'file-loader',
          options: {
            outputPath: '.',
          },
        },
      },
      {
        test: /\.(woff2|ttf)$/,
        use: ['file-loader'],
      },
    ],
  },
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin({extractComments: false})],
  },
  output: {
    filename: (pathData) => {
      return `${_.kebabCase(pathData.chunk.name)}.bundle.js`
    },
    library: '[name]',
    libraryExport: 'default',
    libraryTarget: 'umd',
    path: path.resolve(
      __dirname,
      '../dc-cudami-admin/target/classes/static/js',
    ),
  },
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx'],
  },
}
