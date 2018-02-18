FROM rewatiraman/ytel-data-engineering
VOLUME /app/logs
ADD target/scala-2.12/*assembly*.jar  /app/app.jar
RUN echo "#!/bin/bash" >> /app/start.sh
RUN echo "java -jar -Dconfig.file=/data/myapp.conf /app/app.jar" >> /app/start.sh
RUN chmod +x /app/start.sh
ENTRYPOINT ["/app/start.sh"]