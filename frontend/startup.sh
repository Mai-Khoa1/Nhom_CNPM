#!/bin/sh
set -e

if [ -f /home/site/wwwroot/web.config ]; then
  echo "web.config already exists"
else
  cp /home/site/wwwroot/web.config /home/site/wwwroot/web.config
fi

# Keep container alive
exec /bin/sh -c "while true; do sleep 3600; done"
