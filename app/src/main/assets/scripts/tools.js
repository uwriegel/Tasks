

function extractQuery(urlQuery) {
    var query = decodeURIComponent(urlQuery)
    var map = new Map()
    var queries = query.split("&")
    queries.forEach(n => {
        var vals = n.split("=")
        map[vals[0]] = vals[1]
    })
    return map
}
