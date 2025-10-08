export default {
  parse: (text)=> {
    // naive CSV parser
    return text.split('\n').map(r=> r.split(','))
  }
}