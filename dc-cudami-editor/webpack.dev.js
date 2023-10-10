const {merge} = require('webpack-merge')

const common = require('./webpack.common.js')

module.exports = merge(common, {
  devtool: 'eval-cheap-source-map',
  mode: 'development',
  watch: true,
})
