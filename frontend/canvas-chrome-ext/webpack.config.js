/**
 * Webpack for building the project and creating static
 * versions of the components we need to display on the webpage
 * @type {path.PlatformPath | path}
 */

const path = require('path');
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
    entry: {
        popup: './src/popup.jsx',
        background: './src/background.jsx'
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
    },
    module: {
        rules: [{
            test: /\.(js|jsx)$/,
            exclude: /node_modules/,
            use: {
                loader: 'babel-loader',
                options: {
                    presets: ['@babel/preset-env', '@babel/preset-react'],
                }
            }
        }],
    },
    plugins: [new HtmlWebpackPlugin({
        template: './src/popup.html',
        filename: 'popup.html'
    })],
};