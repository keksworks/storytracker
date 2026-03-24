<script lang="ts">
  import {Link, Route, Router} from './index'
</script>

<Router>
  <Route path="/">
    <h1>Home Page</h1>
    <Link to="/about">About</Link>
  </Route>
  <Route path="/about">
    <h1>About Page</h1>
  </Route>
  <Route path="/user/:id" let:id>
    <h1>User {id}</h1>
  </Route>
</Router>
