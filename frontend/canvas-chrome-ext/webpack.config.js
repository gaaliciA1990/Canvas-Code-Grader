/**
 * Webpack for building the project and creating static
 * versions of the components we need to display on the webpage
 * @type {path.PlatformPath | path}
 */

const path = require('path');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
    entry: {
        popup: './src/popup.jsx',
        background: './src/background.jsx',
        student: './src/scripts/student-content.jsx',
        instructor: './src/scripts/instructor-content.jsx',
        compilationStatus: './src/scripts/compilation-notification.jsx',
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
        template: './src/html/popup.html',
        filename: 'popup.html'
    }),
        new CopyPlugin({
            patterns: [
                {from: "public"},
            ],
        }),
    ],
};