
# Running


Up mongo in a docker to keep your environment clean :-)

`docker run --name crawler-mongo -p 27017:27017 -d mongo`

Build the application

`mvn package`

Run the application

`java -jar target/crawler-0.0.1-SNAPSHOT.jar`


Posting a url:

```
curl -vvv \
-XPOST \
-H "Content-type: application/json" \
-d '[{"url":"https://news.ycombinator.com/"}, {"url":"google.es"}]' \
localhost:8080/url

```


# Bonus

check the results using the HAL browser:

open: [http://localhost:8080/browser/index.html](http://localhost:8080/browser/index.html#http://localhost:8080/urlEntries)

Data Rest & Hal Browser to browse the entities in the mongo repository

# Final Consideration

I used my own technology stack which is mostly based on the tools on the requirements
because I did not have a lot of time to build this app and
I preferred to do that in the amount of time I had than not build it at all